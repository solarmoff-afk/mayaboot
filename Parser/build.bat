@echo off

echo Compile Api Extractor...

echo Compile java code...

javac -cp "libs\*" -d . src\ApiExtractor.java

@echo Launching...

java -cp ".;libs\*" ApiExtractor