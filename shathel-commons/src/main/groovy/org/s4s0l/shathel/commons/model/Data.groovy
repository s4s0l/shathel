package org.s4s0l.shathel.commons.model

/**
 * @author Matcin Wielgus
 */
class Dependency {
    String groupAndProject;
    String expectedVersion;
    String minVersion;
    String maxVersion;
    String classifier = 'shathel'
    String extension = 'zip'
    String getGroup(){
        return groupAndProject.split(":")[0]
    }

    String getName(){
        return groupAndProject.split(":")[1]
    }



}
