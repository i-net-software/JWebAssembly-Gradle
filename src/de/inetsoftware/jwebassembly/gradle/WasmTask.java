/*
 * Copyright 2018 - 2022 Volker Berlin (i-net software)
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
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

/**
 * The wasm task.
 * 
 * @author Volker Berlin
 */
public class WasmTask extends AbstractArchiveTask {

    private String       compilerVersion = "+";

    private OutputFormat format;

    private final Map<String,String> props = new HashMap<>();

    private FileCollection classpath;

    /**
     * Create instance and set initial values.
     */
    public WasmTask() {
        setFormat( OutputFormat.Binary );
    }

    /**
     * Get the JWasmAssembler compiler version.
     * 
     * @return the current version
     */
    @Input
    public String getCompilerVersion() {
        return compilerVersion;
    }

    /**
     * Set the JWasmAssembler compiler version. This can be value like '+', '0.1' or 'com.github.i-net-software:jwebassembly:master-SNAPSHOT'.
     * 
     * @param version
     *            the new version
     */
    public void setCompilerVersion( String version ) {
        compilerVersion = version;
    }

    /**
     * Get the output format.
     * 
     * @return the current format
     */
    @Input
    public OutputFormat getFormat() {
        return format;
    }

    /**
     * Set the compiler output format. Possible values are 'Binary' and 'Text'
     * 
     * @param format
     *            the new format
     */
    public void setFormat( OutputFormat format ) {
        this.format = format;
        if( format == OutputFormat.Binary ) {
            getArchiveExtension().set( "wasm" );
        } else {
            getArchiveExtension().set( "wat" ); // The .wast format is a superset of the .wat format that is intended for writing test scripts.
        }
    }

    /**
     * Generates textual names for function types, globals, labels etc.
     * 
     * @return true, if set
     */
    @Input
    public boolean isDebugNames() {
        return Boolean.parseBoolean( getProperty( "DebugNames" ) );
    }

    /**
     * Generates textual names for function types, globals, labels etc.
     * 
     * @param debugNames
     *            new value
     */
    public void setDebugNames( boolean debugNames ) {
        setProperty( "DebugNames", Boolean.toString( debugNames ) );
    }

    /**
     * Get the source files location.
     * 
     * @return current location
     */
    @Input
    public String getSourceMapBase() {
        String str = getProperty( "SourceMapBase" );
        return str == null ? "" : str;
    }

    /**
     * Property for an absolute or relative path between the final wasm file location and the source files location.
     * If not empty it should end with a slash like "../../src/main/java/".
     * 
     * @param sourceMapBase
     *            new value
     */
    public void setSourceMapBase( String sourceMapBase ) {
        setProperty( "SourceMapBase", sourceMapBase );
    }

    /**
     * Get all properties
     * 
     * @return the properties container
     */
    @Input
    public Map<String, String> getProperties() {
        return props;
    }

    /**
     * Get a property of the compiler
     * 
     * @param key
     *            the name of the property
     * @return the property value
     */
    public String getProperty( String key ) {
        return props.get( key );
    }

    /**
     * Set a property for the compiler. See the compiler documentation of the used version for details.
     * 
     * @param key
     *            the property key
     * @param value
     *            the value
     */
    public void setProperty( String key, String value ) {
        if( value != null ) {
            props.put( key, value );
        } else {
            props.remove( key );
        }
    }

    /**
     * Returns the classpath to use to compile the wasm file.
     *
     * @return The classpath.
     */
    @CompileClasspath
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the classpath to use to compile the wasm file.
     *
     * @param configuration
     *            The classpath. Must not be null, but may be empty.
     */
    public void setClasspath( FileCollection configuration ) {
        this.classpath = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CopyAction createCopyAction() {
        WasmCompiler compiler = new WasmCompiler( this );

        return new CopyAction() {

            @Override
            public WorkResult execute( CopyActionProcessingStream stream ) {
                stream.process( new CopyActionProcessingStreamAction() {

                    @Override
                    public void processFile( FileCopyDetailsInternal details ) {
                        if( !details.isDirectory() ) {
                            compiler.addFile( details.getFile() );
                        }
                    }

                } );

                for( File file : getClasspath().getFiles() ) {
                    compiler.addLibrary( file );
                }

                compiler.compile();
                return WorkResults.didWork( true );
            }
        };
    }
}
