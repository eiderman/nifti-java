# Generated by Buildr 1.4.9, change to your liking


# Version number for this release
VERSION_NUMBER = "1.0.0"
# Group identifier for your projects
GROUP = "nifti-java"
COPYRIGHT = "University of Washington"

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://repo1.maven.org/maven2"

# Dependency Short Forms
FASTUTIL='it.unimi.dsi:fastutil:jar:6.4.6'
VECMATH='java3d:vecmath:jar:1.3.1'

desc "The Nifti-java project"
define "nifti-java" do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
  compile.with FASTUTIL,VECMATH
  package(:jar)
end