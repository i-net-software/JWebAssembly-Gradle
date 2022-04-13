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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

/**
 * The WASM Gradle Plugin class.
 * 
 * @author Volker Berlin
 */
public class WasmPlugin implements Plugin<Project> {

    static final String CONFIGURATION_NAME = "wasmCompiler";

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply( Project project ) {
        project.getPlugins().apply( JavaPlugin.class );
        JavaPluginConvention javaConvention = project.getConvention().getPlugin( JavaPluginConvention.class );
        SourceSet main = javaConvention.getSourceSets().getByName( SourceSet.MAIN_SOURCE_SET_NAME );

        WasmTask wasm = project.getTasks().create( "wasm", WasmTask.class );
        wasm.setDescription( "Assembles a jar archive containing the main classes." );
        wasm.setGroup( BasePlugin.BUILD_GROUP );
        wasm.from( main.getOutput().getClassesDirs() ); // only classes, not resources
        wasm.setClasspath( main.getCompileClasspath() );

        // Create dependencies
        wasm.dependsOn( "classes" );
        Task build = project.getTasks().getByName( "build" );
        build.dependsOn( wasm );

        // Creates the configurations used by plugin.
        Configuration config = project.getConfigurations().create( CONFIGURATION_NAME );
        config.setVisible( false );
        config.setTransitive( true );
        config.setDescription( "The WASM Compiler dependency." );
    }
}
