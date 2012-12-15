nifti-java
==========

Java Based Library for reading Analyze files and Nifti files.  Includes support for old SPM files.

Overview
========

This library was built as a part of [MindSeer](http://sig.biostr.washington.edu/projects/MindSeer/index.html), which is a tool for multi-modal brain visualization.

Our NIFTI library provides support for reading both NIFTI files and the Analyze files produced by SPM (including the extra matrix file) and writing NIFTI files.  It is available under the LGPL.

The bulk of the work is done by the header reader, which provides getters/setters for all of the fields in the header (and uses the both the SPM and NIFTI names for these fields). It also supports automatic decompression of GZipped files.

A Nifti file reader provides some convenience for getting the image file into a useable form. 

To minimize dependencies and maximize usefulness in existing applications, the reader doesn't provide classes for wrapping the data into a Volume structure or doing matrix multiplications to convert between mm and indices.  Our system uses vecmath for the matrix multiplications (available with Java3D as vecmath.jar) and a home brewed volume structure. Our volume structure is in the volume package of MindSeer.

1.1.1 includes some minor bug fixes and performance enhancements and expansion of the support for atlases using an xml metadata file.

They now include better support for sampling voxel values using sample() and sampleNumber(). Just default i5 to 0 for 3 or 4 dimensional files and t to 0 for 3 dimensional files. i5 exists to support 5D NIFTI files that have a matrix or vector at each voxel (e.g. DTI).

Building
========

The project is built using [Apache Buildr](http://buildr.apache.org/).  There is nothing special about the build except for downloading [fastutil](http://fastutil.di.unimi.it/).