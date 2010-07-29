#!/bin/bash
javac -d bin -classpath Pastry/FreePastry-2.1.jar:Pastry/jdom.jar:Pastry/jline-0.9.94.jar:Pastry/xmlpull_1_1_3_4b.jar:Pastry/xpp3-1.1.3.4.O.jar src/*/*.java
cp Pastry/*.jar bin/.
cp manifest bin/.
cd bin
jar cfm ../PastryGrid.jar manifest *
cd ..
rm bin/*.jar
rm bin/manifest
