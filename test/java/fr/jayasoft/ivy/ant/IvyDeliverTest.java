/*
 * This file is subject to the license found in LICENCE.TXT in the root directory of the project.
 * 
 * #SNAPSHOT#
 */
package fr.jayasoft.ivy.ant;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;

import fr.jayasoft.ivy.DependencyDescriptor;
import fr.jayasoft.ivy.Ivy;
import fr.jayasoft.ivy.ModuleDescriptor;
import fr.jayasoft.ivy.ModuleRevisionId;
import fr.jayasoft.ivy.xml.XmlModuleDescriptorParser;

public class IvyDeliverTest extends TestCase {
    private File _cache;
    private IvyDeliver _deliver;
    private Project _project;
    
    protected void setUp() throws Exception {
        cleanTestDir();
        cleanRep();
        createCache();
        _project = new Project();
        _project.setProperty("ivy.conf.file", "test/repositories/ivyconf.xml");
        _project.setProperty("build", "build/test/deliver");

        _deliver = new IvyDeliver();
        _deliver.setProject(_project);
        _deliver.setCache(_cache);
    }

    private void createCache() {
        _cache = new File("build/cache");
        _cache.mkdirs();
    }
    
    protected void tearDown() throws Exception {
        cleanCache();
        cleanTestDir();
        cleanRep();
    }

    private void cleanCache() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(_cache);
        del.execute();
    }

    private void cleanTestDir() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(new File("build/test/deliver"));
        del.execute();
    }

    private void cleanRep() {
        Delete del = new Delete();
        del.setProject(new Project());
        del.setDir(new File("test/repositories/1/jayasoft"));
        del.execute();
    }

    public void testSimple() throws Exception {
        _project.setProperty("ivy.dep.file", "test/java/fr/jayasoft/ivy/ant/ivy-latest.xml");
        IvyResolve res = new IvyResolve();
        res.setProject(_project);
        res.execute();
        
        _deliver.setPubrevision("1.2");
        _deliver.setDeliverpattern("build/test/deliver/ivy-[revision].xml");
        _deliver.execute();
        
        // should have done the ivy delivering
        File deliveredIvyFile = new File("build/test/deliver/ivy-1.2.xml");
        assertTrue(deliveredIvyFile.exists()); 
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(new Ivy(), deliveredIvyFile.toURL(), true);
        assertEquals(ModuleRevisionId.newInstance("jayasoft", "resolve-latest", "1.2"), md.getModuleRevisionId());
        DependencyDescriptor[] dds = md.getDependencies();
        assertEquals(1, dds.length);
        assertEquals(ModuleRevisionId.newInstance("org1", "mod1.2", "2.2"), dds[0].getDependencyRevisionId());
    }

    public void testNoReplaceDynamicRev() throws Exception {
        _project.setProperty("ivy.dep.file", "test/java/fr/jayasoft/ivy/ant/ivy-latest.xml");
        IvyResolve res = new IvyResolve();
        res.setProject(_project);
        res.execute();
        
        _deliver.setPubrevision("1.2");
        _deliver.setDeliverpattern("build/test/deliver/ivy-[revision].xml");
        _deliver.setReplacedynamicrev(false);
        _deliver.execute();
        
        // should have done the ivy delivering
        File deliveredIvyFile = new File("build/test/deliver/ivy-1.2.xml");
        assertTrue(deliveredIvyFile.exists()); 
        ModuleDescriptor md = XmlModuleDescriptorParser.parseDescriptor(new Ivy(), deliveredIvyFile.toURL(), true);
        assertEquals(ModuleRevisionId.newInstance("jayasoft", "resolve-latest", "1.2"), md.getModuleRevisionId());
        DependencyDescriptor[] dds = md.getDependencies();
        assertEquals(1, dds.length);
        assertEquals(ModuleRevisionId.newInstance("org1", "mod1.2", "latest.integration"), dds[0].getDependencyRevisionId());
    }

}
