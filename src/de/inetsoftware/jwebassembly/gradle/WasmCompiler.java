/*
 * Copyright 2018 Volker Berlin (i-net software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.inetsoftware.jwebassembly.gradle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskExecutionException;

/**
 * A wrapper to the downloaded JWebAssembly via reflection.
 * 
 * @author Volker Berlin
 */
class WasmCompiler {

    private static Class<?> jWebAssemblyClass;

    private static Method   addFile;

    private static Method   compileToBinary;

    private static Method   compileToText;

    private WasmTask        task;

    private Object          instance;

    /**
     * Create a new instance of the compiler. To download the right version this must occur lazy after the
     * configuration.
     * 
     * @param task
     *            current task with configuration
     */
    WasmCompiler( WasmTask task ) {
        this.task = task;
        try {
            instance = getCompilerClass( task ).newInstance();
        } catch( Exception ex ) {
            throw new TaskExecutionException( task, ex );
        }
    }

    /**
     * Get the class from JWebAssembly.
     */
    private Class<?> getCompilerClass( WasmTask task ) throws Exception {
        if( jWebAssemblyClass == null ) {
            Project project = task.getProject();

            project.getDependencies().add( WasmPlugin.CONFIGURATION_NAME, "de.inetsoftware:jwebassembly-compiler:" + task.getCompilerVersion() );
            Configuration config = project.getConfigurations().getByName( WasmPlugin.CONFIGURATION_NAME );
            ArrayList<URL> urls = new ArrayList<>();
            for( File file : config.getFiles() ) {
                task.getLogger().lifecycle( "\tcompiler: " + file.getName() );
                urls.add( file.toURI().toURL() );
            }
            URLClassLoader cl = new URLClassLoader( urls.toArray( new URL[0] ) );

            jWebAssemblyClass = cl.loadClass( "de.inetsoftware.jwebassembly.JWebAssembly" );

            addFile = getMethod( "addFile", File.class );
            compileToBinary = getMethod( "compileToBinary", File.class );
            compileToText = getMethod( "compileToText", Appendable.class );
        }
        return jWebAssemblyClass;
    }

    /**
     * Load a method via reflection for later use.
     * 
     * @param name
     *            the method name
     * @param parameterTypes
     *            the parameters
     * @return the requested method
     */
    private static Method getMethod( String name, Class<?>... parameterTypes ) throws Exception {
        Method method = jWebAssemblyClass.getMethod( name, parameterTypes );
        method.setAccessible( true );
        return method;
    }

    /**
     * Add a single file to the compiler
     * 
     * @param file
     *            the file
     */
    void addFile( @Nonnull File file ) {
        try {
            addFile.invoke( instance, file );
        } catch( InvocationTargetException ex ) {
            throw new TaskExecutionException( task, ex.getTargetException() );
        } catch( Exception ex ) {
            throw new TaskExecutionException( task, ex );
        }
    }

    /**
     * Do the compiling to a wasm file.
     */
    void compile() {
        try {
            if( task.getFormat() == OutputFormat.Binary ) {
                compileToBinary.invoke( instance, task.getArchivePath() );
            } else {
                try (OutputStreamWriter output = new OutputStreamWriter( new FileOutputStream( task.getArchivePath() ), StandardCharsets.UTF_8 )) {
                    compileToText.invoke( instance, output );
                }
            }
        } catch( InvocationTargetException ex ) {
            Throwable targetException = ex.getTargetException();
            String msg = "WasmException".equals( targetException.getClass().getSimpleName() ) ? targetException.getMessage() : targetException.toString();
            task.getLogger().error( "> WASM compile failed with: " + msg );
            throw new TaskExecutionException( task, targetException );
        } catch( Exception ex ) {
            throw new TaskExecutionException( task, ex );
        }
    }
}
