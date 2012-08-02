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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.hupo.psi.mi.psicquic.indexing.batch.SolrMitabIndexer;
import org.hupo.psi.mi.psicquic.model.server.SolrJettyRunner;
import org.hupo.psi.mi.psicquic.ws.config.PsicquicConfig;
import org.hupo.psi.mi.psicquic.ws.utils.PsicquicStreamingOutput;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id: SolrBasedPsicquicRestServiceTest.java 17838 2012-01-30 14:23:55Z brunoaranda $
 */
public class SolrBasedPsicquicRestServiceTest {

    private static PsicquicRestService service;

    private static SolrJettyRunner solrJettyRunner;

    @Before
    public void setupSolrPsicquicService() throws Exception {

        // Start a jetty server to host the solr index
        solrJettyRunner = new SolrJettyRunner();
        solrJettyRunner.start();

        // index data to be hosted by PSICQUIC
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"/META-INF/beans.spring.test.xml", "classpath*:/META-INF/psicquic-spring.xml",
                "/META-INF/psicquic-indexing-spring-test.xml"});

        SolrMitabIndexer indexer = (SolrMitabIndexer)context.getBean("solrMitabIndexer");
        indexer.startJob("mitabIndexNegativeJob");

        SolrServer solrServer = solrJettyRunner.getSolrServer();
        Assert.assertEquals(4L, solrServer.query(new SolrQuery("*:*")).getResults().getNumFound());

        PsicquicConfig config = (PsicquicConfig)context.getBean("testPsicquicConfig");
        config.setSolrUrl(solrJettyRunner.getSolrUrl());

	    service = (SolrBasedPsicquicRestService) context.getBean("solrBasedPsicquicRestService");
    }

    @After
    public void after() throws Exception {

        solrJettyRunner.stop();
        solrJettyRunner = null;
        service = null;
    }

    @Test
    public void testGetByInteractor() throws Exception {

        // search for idA or idB
        ResponseImpl response = (ResponseImpl) service.getByInteractor("P07228", "uniprotkb", "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        Assert.assertEquals(2, baos.toString().split("\n").length);

        // search for altidA or altidB
        ResponseImpl response2 = (ResponseImpl) service.getByInteractor("EBI-5606437", "intact", "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso2 = (PsicquicStreamingOutput) response2.getEntity();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        pso2.write(baos2);

        Assert.assertEquals(2, baos2.toString().split("\n").length);

        // search for aliasA or aliasB
        ResponseImpl response3 = (ResponseImpl) service.getByInteractor("RGD-receptor", "uniprotkb", "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso3 = (PsicquicStreamingOutput) response3.getEntity();

        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        pso3.write(baos3);

        Assert.assertEquals(2, baos3.toString().split("\n").length);

        // serch for db only
        ResponseImpl response4 = (ResponseImpl) service.getByInteractor(null, "uniprotkb", "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso4 = (PsicquicStreamingOutput) response4.getEntity();

        ByteArrayOutputStream baos4 = new ByteArrayOutputStream();
        pso4.write(baos4);

        Assert.assertEquals(4, baos4.toString().split("\n").length);

        // serch for id only
        ResponseImpl response5 = (ResponseImpl) service.getByInteractor("RGD-receptor", null, "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso5 = (PsicquicStreamingOutput) response5.getEntity();

        ByteArrayOutputStream baos5 = new ByteArrayOutputStream();
        pso5.write(baos5);

        Assert.assertEquals(2, baos5.toString().split("\n").length);
    }

    @Test
    public void testGetByInteraction() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByInteraction("EBI-5630468", null, "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        Assert.assertEquals(1, baos.toString().split("\n").length);

        // search for db only
        ResponseImpl response2 = (ResponseImpl) service.getByInteraction(null, "intact", "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso2 = (PsicquicStreamingOutput) response2.getEntity();

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        pso2.write(baos2);

        Assert.assertEquals(4, baos2.toString().split("\n").length);

        // search for db:id only
        ResponseImpl response3 = (ResponseImpl) service.getByInteraction("EBI-5630468", "intact", "tab25", "0", "50", "n");

        PsicquicStreamingOutput pso3 = (PsicquicStreamingOutput) response3.getEntity();

        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        pso3.write(baos3);

        Assert.assertEquals(1, baos3.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_count() throws Exception {
        Long response = (Long) service.getByQuery("pmethod:\"western blot\"", "count", "0", "200", "n");

        Assert.assertEquals(new Long(2), response);
    }

    @Test
    public void testGetByQuery_tab25() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "tab25", "0", "200", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        Assert.assertEquals(2, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_tab26() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "tab26", "0", "200", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        Assert.assertEquals(2, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_tab27() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "tab27", "0", "200", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        Assert.assertEquals(2, baos.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_xml25() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "xml25", "0", "200", "n");

        psidev.psi.mi.xml254.jaxb.EntrySet entrySet = (psidev.psi.mi.xml254.jaxb.EntrySet) response.getEntity();

        Assert.assertNotNull(entrySet);
        Assert.assertEquals(2, entrySet.getEntries().iterator().next().getInteractionList().getInteractions().size());
    }

    @Test
    public void testGetByQuery_xgmml() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "xgmml", "0", "200", "n");

        InputStream pso = (InputStream) response.getEntity();

        Assert.assertNotNull(pso);
    }

    @Test
    public void testGetByQuery_biopax() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "biopax", "0", "200", "n");

        InputStream pso = (InputStream) response.getEntity();

        Assert.assertNotNull(pso);

        ResponseImpl response2 = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "biopax-L2", "0", "200", "n");

        InputStream pso2 = (InputStream) response2.getEntity();

        Assert.assertNotNull(pso2);

        ResponseImpl response3 = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "biopax-L3", "0", "200", "n");

        InputStream pso3 = (InputStream) response3.getEntity();

        Assert.assertNotNull(pso3);
    }

    @Test
    public void testGetByQuery_rdf() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "rdf", "0", "200", "n");

        String pso = (String) response.getEntity();

        Assert.assertNotNull(pso);
        Assert.assertEquals("Format not supported: rdf", pso);

        ResponseImpl response2 = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "rdf-xml", "0", "200", "n");

        InputStream pso2 = (InputStream) response2.getEntity();

        Assert.assertNotNull(pso2);

        ResponseImpl response3 = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "rdf-xml-abbrev", "0", "200", "n");

        InputStream pso3 = (InputStream) response3.getEntity();

        Assert.assertNotNull(pso3);

        ResponseImpl response4 = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "rdf-n3", "0", "200", "n");

        InputStream pso4 = (InputStream) response4.getEntity();

        Assert.assertNotNull(pso4);

        ResponseImpl response5 = (ResponseImpl) service.getByQuery("pmethod:\"western blot\"", "rdf-turtle", "0", "200", "n");

        InputStream pso5 = (InputStream) response5.getEntity();

        Assert.assertNotNull(pso5);
    }
    
    @Test
    public void testGetByQuery_tab25_bin() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("EBI-5630468", "tab25-bin", "0", "200", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        // gunzip the output
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);

        ByteArrayOutputStream mitabOut = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0)
            mitabOut.write(buf, 0, len);

        gzipInputStream.close();
        mitabOut.close();

        Assert.assertEquals(1, mitabOut.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_tab26_bin() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("EBI-5630468", "tab26-bin", "0", "200", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        // gunzip the output
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);

        ByteArrayOutputStream mitabOut = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0)
            mitabOut.write(buf, 0, len);

        gzipInputStream.close();
        mitabOut.close();

        Assert.assertEquals(1, mitabOut.toString().split("\n").length);
    }

    @Test
    public void testGetByQuery_tab27_bin() throws Exception {
        ResponseImpl response = (ResponseImpl) service.getByQuery("EBI-5630468", "tab27-bin", "0", "200", "n");

        PsicquicStreamingOutput pso = (PsicquicStreamingOutput) response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pso.write(baos);

        // gunzip the output
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);

        ByteArrayOutputStream mitabOut = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0)
            mitabOut.write(buf, 0, len);

        gzipInputStream.close();
        mitabOut.close();

        Assert.assertEquals(1, mitabOut.toString().split("\n").length);
    }
}
