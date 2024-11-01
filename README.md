# Description

This is a proof-of-concept sample directory manager plugin to showcase login flow by calling an external API with the following flow.

1. The plugin would add an additional login button to the login page in Joget.
2. Clicking on the button would retrieve the username and password of the login form and calls the [webservice](https://github.com/jogetoss/sample-api-authentication/blob/main/src/main/java/org/joget/plugin/marketplace/SampleAPIDirectoryManager.java#L105) method.
3. A API call to external server will be made by calling the [authenticateExternal](https://github.com/jogetoss/sample-api-authentication/blob/main/src/main/java/org/joget/plugin/marketplace/SampleAPIDirectoryManager.java#L105) method.
4. The payload of the external API is read and used to determine if user is authorized or not. Please see https://github.com/jogetoss/sample-api-authentication/blob/main/src/main/java/org/joget/plugin/marketplace/SampleAPIDirectoryManager.java#L171.
5. If the user is **authenticated**, and the user exists in Joget, then the user is logged in.
6. If the user is **authenticated**, but the user does **not** exists in Joget, and the user provisioning feature is **enabled** in the plugin, then a new Joget user will be created and the user is logged in. Please see https://github.com/jogetoss/sample-api-authentication/blob/main/src/main/java/org/joget/plugin/marketplace/SampleAPIDirectoryManager.java#L200.
7. If the user is **authenticated**, but the user does **not** exists in Joget, and the user provisioning feature is **disabled** in the plugin, then login fails. Please see https://github.com/jogetoss/sample-api-authentication/blob/main/src/main/java/org/joget/plugin/marketplace/SampleAPIDirectoryManager.java#L236.

Get Started

1. Clone this repository and modify the plugin accordingly.
2. The external API URL is configured at https://github.com/jogetoss/sample-api-authentication/blob/main/src/main/java/org/joget/plugin/marketplace/SampleAPIDirectoryManager.java#L138.
3. Build the plugin and test.

# Getting Help

JogetOSS is a community-led team for open source software related to the [Joget](https://www.joget.org) no-code/low-code application platform.
Projects under JogetOSS are community-driven and community-supported.
To obtain support, ask questions, get answers and help others, please participate in the [Community Q&A](https://answers.joget.org/).

# Contributing

This project welcomes contributions and suggestions, please open an issue or create a pull request.

Please note that all interactions fall under our [Code of Conduct](https://github.com/jogetoss/repo-template/blob/main/CODE_OF_CONDUCT.md).

# Licensing

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

NOTE: This software may depend on other packages that may be licensed under different open source licenses.
