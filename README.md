# Icons for Jira v9.x.x
*original link* https://marketplace.atlassian.com/archive/1214988

### The addon was adapted for Jira 9.x.x version. The source files have been decompiled and adapted to take into account new updates.

*Solved:*
The options settings tab has been fixed.
The ability to select preloaded icons has been fixed.

*Not fixed:*
Ability to upload your own images

## Installation

*You must have it installed:*
- Java SDK 17
- Atlassian SDK 8.7.2^ https://developer.atlassian.com/server/framework/atlassian-sdk/

1. clone https://github.com/Jahimees/icon-select-addon-jira9-codebarrel.git 
2. Open cmd console and go to `path/to/the/project/base-pom/iconselect`
3. Use command 'atlas-clean package' - it will create .jar and .obr files
4. Go to your Jira instance in browser. (How to set up the jira server instance https://developer.atlassian.com/server/framework/atlassian-sdk/create-a-helloworld-plugin-project/)
5. Go to the admin panel (Manage apps)
6. Click **'upload app'** and choose your .jar or .obr file (obr file can work incorrect. Jar file is prefer)
7. Go to the **Admin - Issues - Custom Fields - Add custom field**. In Advanced section you can see 2 new field types 'Icon multi select' and 'Icon single select'

![image](https://github.com/user-attachments/assets/af2894df-d353-4fa7-bdd3-f32429ba4d0a)
![image](https://github.com/user-attachments/assets/eaf9e90c-4fa4-4aef-957d-e6f17b5cebdc)
![image](https://github.com/user-attachments/assets/93aa5edf-6bc4-4ffe-9b9b-8191a5c91efb)
![image](https://github.com/user-attachments/assets/8a45ef8b-914b-4e55-9498-9b20765d4945)


All rights belong to the original developer
