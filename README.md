# IdrRestartSchedulingTool
Tool to periodically check status on IDR replication and restart them if necessary

To configure:

<ol>
  <li>resources/log4j2.xml controls logging characteristics such as level of logging and log file name and sizes</li>
  <li>resources/configurationFile.xml presents a sample configuration file</li>
  <li>resources/key.dat AES key file.
</ol>

<p>Two jar files are provided: IdrRestartSchedulingTool-0.0.1-jar-with-dependencies.jar IdrRestartSchedulingTool-0.0.1 without dependencies</p>
<p>To use this program you will need an IDR user with monitoring priviledges on the datastores where subscriptions belongs. The password has to be encrypted.  The com.demo.management.idr.util.Encryptor class can be used to encrypt your password before editing the configuration file.</p>
<p>to use this program call nohup java -cp IdrRestartSchedulingTool-0.0.1-jar-with-dependencies.jar com.demo.management.idr.scheduler.IdrSchedulerService configuration_file.xml &</p>

