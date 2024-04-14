@echo off
::
:: Licensed to the Apache Software Foundation (ASF) under one or more
:: contributor license agreements.  See the NOTICE file distributed with
:: this work for additional information regarding copyright ownership.
:: The ASF licenses this file to you under the Apache License, Version 2.0
:: (the "License"); you may not use this file except in compliance with
:: the License.  You may obtain a copy of the License at
::
:: http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::

:: sqlline.bat - Windows script to launch SQL shell
:: Example:
:: > sqlline.bat
:: sqlline> !connect jdbc:calcite: admin admin

:: The script updates the classpath on each execution,
:: You might add CACHE_SQLLINE_CLASSPATH environment variable to cache it
:: To build classpath jar manually use gradlew buildSqllineClasspath
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set CP=%DIRNAME%\target\classes

if not exist "%CP%" (call "%DIRNAME%\mvnw" package --quiet)

@setlocal EnableExtensions EnableDelayedExpansion
set CP=!CP!;%DIRNAME%\target\test-classes
for /F %%i in ('dir /a-d/b/s ..\target\libs\*.jar') do set CP=!CP!;%%i

java -Xmx1g -jar "%CP%" %*
