# WebWhitelist Plugin for Spigot

---

This is a simple plugin that allows you to whitelist players onto your Minecraft server through HTTP requests.

## Building

I wrote this plugin using IntelliJ but the same process should apply for Eclipse. All you need to do is add the current version of Spigot to your build path and then build as jar.

If you have problems or have a better way of standardizing the build process, just make a pull request.

## Setup

To set up the plugin you will need to run it once. The console will tell you to fill out the config.yml file. 

~~~
host: 127.0.0.1 # the IP of your server
port: 3000
secret-key: # some long, random string of characters

webserver:
  useCORS: yes # applies CORS headers and adds OPTIONS methods to created routes
~~~

After you have done so, restart the server (or plugin if you can do so) and the console will tell you it is now listening on the configured `host` and `port`.

The `secret-key` configuration, found in the config.yml file, should be set to a long, random string of characters (I recommend at least a minimum of 24 characters).

---

## General Usage

When you make a request to the server, make sure you send your secret key in the `X-Secret-Key` header. Any request to the default 3 routes (except for OPTIONS requests) will return a `401 Unauthorized` if the key is not present or is invalid.

| Method | Path                            | Request Body                                                          | Status                                                                                                                                                                                                                                                                          | Response Body                                                 |
|:------:|---------------------------------|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
|  POST  | /requests                       | `Content-Type: application/json`<br/>`Body: {"uuid":<player's UUID>}` | <ul><li>201 - Whitelist request was created</li><li>400 - Invalid UUID</li><li>403 - Player is already whitelisted</li><li>409 - Request has already been made</li></ul>                                                                                                        | On 201 and 409:<br/>`{"expiration": <expiration timestamp>}`  |
|  POST  | /whitelist                      | `Content-Type: application/json`<br/>`Body: {"code":<player's code>}` | <ul><li>200 - Player was whitelisted successfully</li><li>400 - Request did not contain a code</li><li>404 - Code is invalid</li><li>409 - External factors prevented the player from being whitelisted</li><li>500 - The player's information could not be retrieved</li></ul> | On 200:<br/>`{"id":<player's UUID>, "name": <player's name>}` |
| DELETE | /whitelist?uuid=<player's UUID> | Empty                                                                 | <ul><li>204 - Player was un-whitelisted successfully</li><li>400 - Invalid UUID</li><li>409 - External factors prevented the player from being un-whitelisted</li></ul>                                                                                                         | Empty                                                         |

Aside from these 3 routes, there is also `GET /`, which always returns `200 - OK`. This can be useful when checking if the web server, or the Minecraft server itself, is running or not.

The general process would go as follows:
1. A user requests to have their player whitelisted from your website. `POST /requests` is requested with their player's UUID.
    - Their UUID will most likely be requested independently via the Mojang API.
2. The user logs onto the Minecraft server and is kicked with a message containing their whitelist code.
3. The user returns to your website and enters their whitelist code. `POST /whitelist` is requested with this code. If successful, the user can return to the server where they should now be whitelisted.

If you need to un-whitelist a player, you only need to request `DELETE /whitelist?uuid=<player's UUID>`

## Library Usage

You can also use this plugin your own plugins by applying a callback functions when the player was whitelisted or when a player was un-whitelisted.

Once you include this plugin into your own plugin's build path, you can add the callbacks like so:

~~~
WebWhitelist.plugin().doOnPlayerWhitelist((profile, requestBody) -> {
   # Your code here
})

WebWhitelist.plugin().doOnRemoveWhitelist(profile -> {
   # Your code here
})
~~~

The parameter, `profile`, here, is of type `org.bukkit.profile.PlayerProfile` and `requestBody` is of type `com.google.gson.JsonElement`.

`requestBody` is the raw JSON body that was sent in the `POST /whitelist` request. You can send and receive any extra data you need in here.

Make sure when you include this plugin to also add it as a dependency in your `plugin.yml`:

~~~
name: <plugin name>
main: <main class>
api-version: <API version>
depend: [WebWhitelist]
~~~

**PLEASE NOTE**

The web server does not yet have support for HTTPS. This may change in a future version.