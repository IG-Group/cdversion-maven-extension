# cdversion-maven-extension

[![CircleCI](https://circleci.com/gh/IG-Group/cdversion-maven-extension.svg?style=svg)](https://circleci.com/gh/IG-Group/cdversion-maven-extension)

## Usage

Add a file to the project root called `.mvn/extensions.xml`. 

Put the following into it:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<extensions>
    <extension>
        <groupId>com.ig.maven.cdversion</groupId>
        <artifactId>cdversion-maven-extension-git</artifactId>
        <version>1.1.1</version>
    </extension>
</extensions>
```

Then change the version of your project to `${revision}`. To make th build work with maven versions that don't support the extension, define the property as well:
```xml
<properties>
    <revision>SNAPSHOT</revision>
</properties>
``` 

