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

import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
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

    private boolean      debugNames;

    private String       compilerVersion = "+";

    private OutputFormat format          = OutputFormat.Binary;

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
     * Set the JWasmAssembler compiler version.
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
    }

    /**
     * Generates textual names for function types, globals, labels etc.
     * 
     * @return true, if set
     */
    @Input
    public boolean isDebugNames() {
        return debugNames;
    }

    /**
     * Generates textual names for function types, globals, labels etc.
     * 
     * @param debugNames
     *            new value
     */
    public void setDebugNames( boolean debugNames ) {
        this.debugNames = debugNames;
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
                compiler.compile();
                return WorkResults.didWork( true );
            }
        };
    }
}
