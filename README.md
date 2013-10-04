GrayTin
=======

Gray Tin is an eclipse tool that helps to synchronize source code within Jenkins builds.

More documentation is coming soon!

Initial Road map
===============
1. Provide all required materials to be able to attract other developers and community for father development
2. Create continuos integration and update site of the project
3. Enable option to provide more connectors for other team providers like Git (or SVN Subclipse), currently is supported only SVN Subversive.
4. Create proper road map based on the community feedback

Thanks 
======
The project has been migrated from an internal to the open source project with kind support of  Be Informed company.

Especially I'd like to say my thanks to Dobias van Buuren and Rachid Ben Moussa who have inspirited & helped me with the development of GrayTin.

Andrej Zachar
Author
  

How to develop GrayTin?
========================

1. In order to develop please download eclipse kepler for instance and setup a git clone of this repo.
2. To prevent license issues the binaries of other projects are included in this repo.
Therefore please create a target-platform project with the following structure (missing libraries please download from the Internet).

target-platform
├── eclipse-platform-indigo
│   ├── Subversive-connectors-2.3.0.I20120520-1700
│   ├── Subversive-incubation-0.7.9.I20110819-1700
│   ├── delta-pack-3.7.2
│   ├── eclipse-rcp-indigo-SR2-macosx-cocoa-x86_64
│   ├── mylyn-3.8.4.v20130429-0100
│   └── porting
│       ├── org.apache.commons.lang_2.6.0.v201205030909.jar
│       └── org.slf4j.api_1.7.2.v20121108-1250.jar
├── eclipse-platform-juno
│   ├── Subversive-1.0.0.I20130122-1700
│   ├── Subversive-connectors-allplatforms-3.0.1.I20130507-1700
│   └── eclipse-rcp-juno-SR2-macosx-cocoa-x86_64
├── eclipse-platform-kepler
│   ├── Subversive-1.1.0.I20130527-1700
│   ├── eclipse-rcp-kepler-R-macosx-cocoa-x86_64
│   └── org.eclipse.pde.source-4.3
├── felix
│   └── org.apache.felix.dependencymanager-3.1.1-SNAPSHOT.jar
├── sdk-tools
│   ├── com.google.guava_14.0.1.jar
│   └── joda-time_1.6.2.jar

3. Relead your new target platform based on your distribution

org.graytin.feature
├── targetPlatformSDK-indigo.target
├── targetPlatformSDK-juno.target
└── targetPlatformSDK-kepler.target


How to build Gray Tin?
======================

Creating (local) update sites
-----------------------------
 
1. Please set up project as mentioned in the paragraph above. 
  
2. Builds are available for Indigo, Juno and Kepler
   In order to build it you need to open (+) or close (-) the following projects:
   
   a) Indigo
   
   	+org.graytin.jenkins.indigo
   	+org.graytin.feature
    
	-org.graytin.jenkins.eclipse4

	-org.graytin.feature.juno
	-org.graytin.feature.kepler

	-org.graytin.updatesite.juno
	-org.graytin.updatesite.kepler
	
    3. reload target platform /org.graytin.feature/targetPlatformSDK-indigo.target
	
	4. open /org.graytin.updatesite/site.xml 
	
	5. add org.graytin.feature_1.0.xxxx under the existing category org.graytin
	
	6. press build all
	
	7. upload results ( 
		/org.graytin.updatesite/features
        /org.graytin.updatesite/plugins
        /org.graytin.updatesite/artifacts.jar
        /org.graytin.updatesite/content.jar
        )
	to your update site or your local update site.
	
	8. open your eclipse and add your update site
	9. install your new GrayTin
	
	
 b) Juno

 	-org.graytin.jenkins.indigo
   	-org.graytin.feature
   	-org.graytin.updatesite
    
	+org.graytin.jenkins.eclipse4

	+org.graytin.feature.juno
	-org.graytin.feature.kepler

	+org.graytin.updatesite.juno
	-org.graytin.updatesite.kepler
	
	3. reload target platform /org.graytin.feature/targetPlatformSDK-juno.target
	
	4. open /org.graytin.updatesite.juno/site.xml
	
	5. add org.graytin.feature_1.0.xxxx under the existing category org.graytin
	
	6. press build all
	
	7. upload results ( 
		/org.graytin.updatesite.juno/features
        /org.graytin.updatesite.juno/plugins
        /org.graytin.updatesite.juno/artifacts.jar
        /org.graytin.updatesite.juno/content.jar
        )
	to your update site or your local update site.
	
	8. open your eclipse and add your update site
	9. install your new GrayTin
	
	   	
 c) Kepler

 	-org.graytin.jenkins.indigo
   	-org.graytin.feature
   	-org.graytin.updatesite
    
	+org.graytin.jenkins.eclipse4

	-org.graytin.feature.juno
	+org.graytin.feature.kepler

	-org.graytin.updatesite.juno
	+org.graytin.updatesite.kepler
	
	3. reload target platform /target-platform/targetPlatformSDK-kepler.target
	
	4. open /org.graytin.updatesite.kepler/site.xml
	
	5. add org.graytin.feature_1.0.xxxx under the existing category org.graytin
	
	6. press build all
	
	7. upload results ( 
		/org.graytin.updatesite/features
        /org.graytin.updatesite/plugins
        /org.graytin.updatesite/artifacts.jar
        /org.graytin.updatesite/content.jar
        )
	to your update site or your local update site.
	
	8. open your eclipse and add your update site
	9. install your new GrayTin
	
	
