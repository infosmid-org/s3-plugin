# 🚀 Version: ${VERSION}

## What's New

- Migrated to 'org.infosmid.plugins'. The plugin id is now `org.infosmid.plugins.s3`.
- The Java package is now `org.infosmid.plugins.gradle.s3` and `org.infosmid.gradle.s3` for the tasks. 
- Build moved to Gradle 9, JUnit 5 and a version catalog.

## 📦 Artifacts

Gradle: 
```groovy
plugins { 
    id 'org.infosmid.plugins.s3' version '${VERSION}'
}
```
