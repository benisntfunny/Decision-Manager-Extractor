**CODE STILL IN PROGRESS THIS PROJECT DOES NOT RUN CORRECTLY YET**

# Decision Manager Data Extraction and Reload Utility

**When/How to use**
* **WHEN** You make changes to an existing Decision Manager instance and want to persist existing data
* **HOW** Prior to running remove scripts run this and have it point to your "load ils.sql" file generated after building your clm project. The utility will read the configured database and compare it to your "load ils.sql" file and generate an insert script for your existing data.



Add config.properties to base directory

**config.properties**
```properties
databaseUser=EXAMPLEUSER1
databaseUserPassword=PASSWORD
databaseUrl=jdbc:oracle:thin:@ldap://server:port/HOSTNAME,cn=OracleContext,dc=domain,dc=com
outputfile=insert_data.sql
tableOwner=EXAMPLEUSER1
oracleLoadScriptLocation=load ils.sql
```
