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

package uk.ac.ebi.ampt2d.metadata.importer.database;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException;
import uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication;
import uk.ac.ebi.ampt2d.metadata.importer.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraAnalysisXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Analysis;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Sample;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.AnalysisRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ReferenceSequenceRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.SampleRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.StudyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ena.sra.xml.AnalysisType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:application.properties", properties = {"import.source=DB"})
@ContextConfiguration(classes = {MetadataImporterMainApplication.class})
public class SraObjectsImporterThroughDBTest {

    private static final String ANALYSIS_DOCUMENT_BIG_XML = "analysis/AnalysisDocumentBig.xml";

    @Autowired
    private ObjectsImporter sraObjectImporter;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ReferenceSequenceRepository referenceSequenceRepository;

    @Autowired
    private TaxonomyRepository taxonomyRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Before
    public void setUp() {
        analysisRepository.deleteAll();
        studyRepository.deleteAll();
        referenceSequenceRepository.deleteAll();
        sampleRepository.deleteAll();
        taxonomyRepository.deleteAll();
    }

    @Test
    @Category(OracleDbCategory.class)
    public void importStudy() throws Exception {
        Study study = sraObjectImporter.importStudy("ERP000860");
        assertNotNull(study);
        assertEquals("ERP000860", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(9999, 12, 31), study.getReleaseDate());
        assertEquals("UK10K_OBESITY_SCOOP", study.getName());

        study = sraObjectImporter.importStudy("ERP000858");
        assertNotNull(study);
        assertEquals("ERP000858", study.getAccessionVersionId().getAccession());
        assertEquals(LocalDate.of(9999, 12, 31), study.getReleaseDate());
        assertEquals("UK10K_NEURO_MUIR", study.getName());

        //studies and analysis aren't imported when source is database and if we start with importStudy
        assertEquals(0, studyRepository.count());
        assertEquals(0, analysisRepository.count());
    }

    @Test
    @Category(OracleDbCategory.class)
    public void importAnalysisObject() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ092698");
        assertNotNull(analysis);
        assertEquals("ERZ092698", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
        assertEquals(1, analysis.getFiles().size());
        assertEquals("ERP009248", analysis.getStudy().getAccessionVersionId().getAccession());
        ReferenceSequence referenceSequence = analysis.getReferenceSequences().get(0);
        assertEquals("GCA_000001405.1", referenceSequence.getAccession());
        assertEquals("GRCh37", referenceSequence.getName());
        assertNull(referenceSequence.getPatch());
        assertEquals(1, studyRepository.count());
        assertEquals(1, analysisRepository.count());
        assertEquals(1, referenceSequenceRepository.count());
    }

    @Test
    @Category(OracleDbCategory.class)
    public void importAnalysisObjectWithReferenceAlignmentType() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ000275");
        assertNotNull(analysis);
        assertEquals("ERZ000275", analysis.getAccessionVersionId().getAccession());
        assertEquals(Analysis.Technology.UNSPECIFIED, analysis.getTechnology());
        assertEquals(1, analysis.getFiles().size());
        assertEquals("ERP001373", analysis.getStudy().getAccessionVersionId().getAccession());
        List<ReferenceSequence> referenceSequences = analysis.getReferenceSequences();
        referenceSequences.parallelStream().allMatch(referenceSequence -> referenceSequence.getType().equals
                (ReferenceSequence.Type.SEQUENCE));
        assertEquals(1, studyRepository.count());
        assertEquals(1, analysisRepository.count());
        assertEquals(25, referenceSequenceRepository.count());
        assertEquals(4, taxonomyRepository.count());

    }

    @Test(expected = AnalysisWithoutReferenceSequenceException.class)
    @Category(OracleDbCategory.class)
    public void importAnalysisObjectWithEmptyReferenceSequenceAccession() throws Exception {
        Analysis analysis = sraObjectImporter.importAnalysis("ERZ000011");
        assertNull(analysis);

        // Analysis is not saved to database as it doesn't have a referenceSequence.
        assertEquals(0, analysisRepository.count());
        assertEquals(0, referenceSequenceRepository.count());
    }

    @Test
    @Category(OracleDbCategory.class)
    public void importSamplesObject() throws Exception {
        SraXmlParser<AnalysisType> analysisTypeSraXmlParser = new SraAnalysisXmlParser();
        String xmlString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(ANALYSIS_DOCUMENT_BIG_XML).toURI())));
        AnalysisType analysisType = analysisTypeSraXmlParser.parseXml(xmlString, "ERZ015710");
        List<Sample> sample = sraObjectImporter.importSamples(analysisType);
        assertEquals(1092, sample.size());
        assertEquals("SRS000031", sample.get(0).getAccessionVersionId().getAccession());
        assertEquals("NA06986", sample.get(0).getName());
        assertEquals("SAMN00797025", sample.get(0).getBioSampleAccession());
        assertEquals("SRS074380", sample.get(1091).getAccessionVersionId().getAccession());
        assertEquals("HG01626", sample.get(1091).getName());
        assertEquals("SAMN00014429", sample.get(1091).getBioSampleAccession());
        assertEquals(1092, sampleRepository.count());
        sample.clear();
        sample = sraObjectImporter.importSamples(analysisType);
        assertEquals(1092, sample.size());
        assertEquals("SRS000031", sample.get(0).getAccessionVersionId().getAccession());
        assertEquals("NA06986", sample.get(0).getName());
        assertEquals("SAMN00797025", sample.get(0).getBioSampleAccession());
        assertEquals("SRS074380", sample.get(1091).getAccessionVersionId().getAccession());
        assertEquals("HG01626", sample.get(1091).getName());
        assertEquals("SAMN00014429", sample.get(1091).getBioSampleAccession());
        assertEquals(1092, sampleRepository.count());
    }

}