/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hupo.psi.mi.psicquic.ws;

import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.hupo.psi.mi.psicquic.ws.config.PsicquicConfig;
import org.hupo.psi.mi.psicquic.ws.utils.PsicquicStreamingOutput;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import psidev.psi.mi.search.Searcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IndexBasedPsicquicRestServiceTest {

    private static IndexBasedPsicquicRestService service;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InputStream mitabStream = IndexBasedPsicquicServiceTest.class.getResourceAsStream("/META-INF/brca2.mitab.txt");
        File indexDir = new File("target", "brca-mitab.index");

        Searcher.buildIndex(indexDir, mitabStream, true, true);

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/META-INF/beans.spring.test.xml"});
        PsicquicConfig config = (PsicquicConfig)context.getBean("psicquicConfig");
        config.setIndexDirectory(indexDir.toString());

	    service = (IndexBasedPsicquicRestService)context.getBean("indexBasedPsicquicRestService");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        service = null;
    }

    @Test
    public void testGetByQuery() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("FANCD1", "psi-mi/tab25", "0", "200");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);
        
        Assert.assertEquals(12, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_maxResults() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("FANCD1", "psi-mi/tab25", "0", "3");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        Assert.assertEquals(3, baos.toString().split("\n").length);
    }
}