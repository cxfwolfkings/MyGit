@echo off
ping -n 1 10.30.100.101 > nul
if errorlevel 1 (echo 101�����쳣) else (echo 101��������)
ping -n 1 10.30.100.102 > nul
if errorlevel 1 (echo 102�����쳣) else (echo 102��������)
ping -n 1 10.30.100.103 > nul
if errorlevel 1 (echo 103�����쳣) else (echo 103��������)
ping -n 1 10.30.100.104 > nul
if errorlevel 1 (echo 104�����쳣) else (echo 104��������)
ping -n 1 10.30.100.105 > nul
if errorlevel 1 (echo 105�����쳣) else (echo 105��������)
ping -n 1 10.30.100.106 > nul
if errorlevel 1 (echo 106�����쳣) else (echo 106��������)
ping -n 1 10.30.100.107 > nul
if errorlevel 1 (echo 107�����쳣) else (echo 107��������)
pause