Settings
--------

You should hide the username/password in your `settings.xml`. Use `settingsKey` in your POM as a lookup key.

	<settings>
	  [...]
	  <servers>
	    <server>
	      <id>serverId</id>
	      <username>username</username>
	      <password>password</password>
	    </server>
	    [...]
	  </servers>
	  [...]
	</settings>

Encrypted passwords
-------------------

  It's also possible to use encrypted passwords. Follow the instructions in the [encryption mini guide](http://maven.apache.org/guides/mini/guide-encryption.html).
  Just like unencrypted passwords you have to be sure to set the `settingsKey`.
  
  If the password can be decypted the plugin will do so, otherwise the raw password will be returned.
   
