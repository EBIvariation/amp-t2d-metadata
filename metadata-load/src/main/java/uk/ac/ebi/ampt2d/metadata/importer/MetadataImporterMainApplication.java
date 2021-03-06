/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
 *
 */

package uk.ac.ebi.ampt2d.metadata.importer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.ampt2d.metadata.importer.api.SraObjectsImporterThroughApi;
import uk.ac.ebi.ampt2d.metadata.importer.database.SraObjectsImporterThroughDatabase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@SpringBootApplication
public class MetadataImporterMainApplication implements ApplicationRunner {

    private static final String ACCESSION_FILE_PATH = "accessions.file.path";

    private static final Logger LOGGER = Logger.getLogger(MetadataImporterMainApplication.class.getName());

    private ObjectsImporter objectsImporter;

    public MetadataImporterMainApplication(ObjectsImporter objectsImporter) {
        this.objectsImporter = objectsImporter;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MetadataImporterMainApplication.class, args);
    }

    /**
     * This method executes the task of importing studies or analyses based on import source.
     * We are starting with analyses in case of objects import through database because the study xmls in database
     * do not contain analysis accessions to import.
     *
     * @param applicationArguments
     */
    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        Set<String> accessions = readAccessionsFromFile(applicationArguments);
        if (objectsImporter instanceof SraObjectsImporterThroughDatabase) {
            accessions.forEach(accession -> {
                try {
                    // TODO: EVA will want to import some projects from the DB too
                    objectsImporter.importAnalysis(accession);
                } catch (Exception exception) {
                    LOGGER.severe("Encountered Exception for analysis accession " + accession);
                    LOGGER.severe(exception.getMessage());
                }
            });
        } else if (objectsImporter instanceof SraObjectsImporterThroughApi) {
            accessions.forEach(accession -> {
                try {
                    objectsImporter.importProject(accession);
                } catch (Exception exception) {
                    LOGGER.severe("Encountered Exception for project accession " + accession);
                    LOGGER.severe(exception.getMessage());
                }
            });
        } else {
            throw new RuntimeException("ObjectsImporter instance not known/supported");
        }
    }

    private Set<String> readAccessionsFromFile(ApplicationArguments applicationArguments) {
        List<String> accessionsFilePath = applicationArguments.getOptionValues(ACCESSION_FILE_PATH);
        if (accessionsFilePath == null || accessionsFilePath.size() == 0) {
            LOGGER.severe("Please provide accessions.file.path");
            throw new RuntimeException("Please provide accessions.file.path");
        }
        String accessionFilePath = accessionsFilePath.get(0);
        Set<String> accessions;
        try {
            accessions = new HashSet<>(Files.readAllLines(Paths.get(accessionFilePath)));
        } catch (NullPointerException exception) {
            String message = "Provided file path is invalid/file does not exists";
            LOGGER.severe(message);
            throw new RuntimeException(message);
        } catch (IOException exception) {
            String message = "Provided file is not valid/corrupt";
            LOGGER.severe(message);
            throw new RuntimeException(message);
        }

        return accessions;
    }
}
