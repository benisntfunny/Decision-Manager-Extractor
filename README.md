# Decision Manager Data Extraction and Reload Utility

**When/How to use**
* **WHEN** You make changes to an existing Decision Manager instance and want to persist existing data
* **HOW** Prior to running remove scripts run this and have it point to your _load ils.sql_ file generated after building your clm project. The utility will read the configured database and compare it to your _load ils.sql_ file and generate an insert script for your existing data.

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

## EXACT EXECUTION ORDER
1. Make CLM project
2. Deploy _clm.ear_ to file system.
3. Run **this** and save output script
4. Run _drop_core.sql_
5. Run _drop ils.sql_
6. Run _insert core.sql_
7. Run _insert ils.sql_
8. Run _insert ils config.sql_
9. Run __output script__ from **this**