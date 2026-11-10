@echo off
setlocal
if exist "C:\Users\sidha\.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    call "C:\Users\sidha\.maven\apache-maven-3.9.6\bin\mvn.cmd" %*
) else (
    call mvn %*
)
