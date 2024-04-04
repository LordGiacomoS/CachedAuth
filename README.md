## Cached Auth
Cached Auth is an unfinished client-side [Fabric][fabricmc_net] mod I am working on that will use a device-code implementation of Minecraft's authentication api to allow users to set up accounts that are saved long term (similar to how [MultiMC][multi_mc_launcher]'s account saving works).

Currently, it is being developed with the intention of personal use, and is unfinished. As such, it isn't functional, and the codebase and git commitment notes contain some useless code along with comments written by someone in a mildly unhinged state.

### Note about Security
As of now, none of the obfuscation and encryption (for login information & mod api secrets) that I'd like to have is implemented, and as such, even once I have a "working" version, this mod will stay in a "please don't use" state

### Credits
Some mods, projects, and documentation created by others was used as reference material/inspiration when making this mod, though I don't believe I directly copy-pasted anything unique. Below is an incomplete list of all that.
- [MultiMC][multi_mc_launcher]: The device login and saving accounts was inspired by them... initially, this project was intended to directly work off of the same data of saved accounts as MultiMC, but due to legal issues, I had to not do that if I wanted to share the code publicly
- [Mod Menu][mod_menu_mod]: Used as a reference when creating the config system for this mod
- [Auth Me][authme_mod]: Used as a reference for the authentication protocols (and writing this readme)
- [DevLogin][dev_login_tool]: Used as a reference for authentication protocols
- [Unofficial Mojang API Documentation][gapple_pw_mojang_api_docs]: Used as reference for the authentication protocols
- [wiki.vg][wiki_vg_microsoft_auth]: Used as a reference for authentication protocols
- [Microsoft Entra OAuth v2 Documentation][microsoft_entra_docs]: Used as reference for the first couple steps of authenticating a microsoft account
#### Similar Mods
(These mods actually currently work, and were a source of inspiration for this project as they are useful, but I wanted a combination of existing features from multiple mods all in one).
- [Auth Me][authme_mod] by axieum
- [ReAuth][reauth_mod] by TechnicianLP
- [OAuth][oauth_mod] by Sintinium


## To-Do list
Most of this stuff is roughly ordered by priority

### to get mod "working"
- [ ] finish oauth implementation and reauthenticating the client
  - [x] Microsoft Oauth2 refresh token flow

  - [x] Microsoft Oauth2 device code flow request
  - [x] Microsoft Oauth2 device code flow polling
  - [x] Xbox Live Authentication
  - [x] XSTS Token for minecraft
  - [ ] Authenticate with minecraft (WIP)
  - [ ] Check game ownership
  - [ ] fetch minecraft profile
- [ ] set up config saving/loading system to store client credentials (use JSON config)
  - [x] rough layout for saved account
  - [ ] support for multiple accounts
  - [ ] integrate with fabric
- [ ] create and implement a gui into minecraft
  - [ ] Basic GUI and textures
  - [ ] Mod Menu integration

### Future plans
- clean up source code from development
  - keep my development comments alive in a development branch but filter it all out from master/release branch (comments are useful as documentation and debugging info)
  - split `README.md` into 2, with one file having basic overview and the other having a much more comprehensive summary (maybe embed 2nd file into first as spoiler?)
  - revisit error handling and make it actually good (do I really need custom exception? should I make more?)
  - remove unnecessary stuff from authentication json parsers & implement datetime handling where necessary
  - move some of the authenticator stuff into AuthorizationSession and other relevant classes(?)
  - convert json deserialization to `JsonHelper.deserialize` method instead of doing it all with individual classes?
- Make the mod actually somewhat secure and protected against casual attackers
  - adjust access modifiers to be as narrow and limiting as possible
  - obfuscation of token info & client id? (very hard to actually encrypt it in any secure manner since I'd still need to have both the encryption & decryption keys accessible in the code -- could send that stuff to a webserver of my own for encryption/decryption, and have it be very picky about packet format and not explain why -- ala xbox xbl api -- but that would introduce additional security issues for something that wouldn't do much)
  - add section to README about how to raise security concerns privately
- allow for custom oauth urls & endpoints (similar to what Auth Me has)
  - document how users can set their own oauth endpoints in case they don't trust my app (also useful for general documentation)
    - add documentation for what settings need to exist when setting up Entra app (include info about registering app with minecraft api)
    - add documentation for what needs to be done in-game
    - add documentation (probs postman) for api flow
- Add debug mode
- publish to modrinth & maybe curseforge (heard questionable things about them, but given how the mod is opensource, if I don't publish it there, someone else probably would)
- forge/neoforge versions? (need to bring myself up to date about what's going on there)
- add those fancy badges people have that show downloads & stuff to the readme
- Offline only usernames support? (mostly useless except for allowing players to mess around with username-specific features like Notch's apple drop...) 

## License & Legal stuff
Cached Auth is open-sourced software licensed under the [MIT license][license]
Essentially all this says is that I'm not responsible for consequences of the use/misuse of this code (including hacked accounts), but will allow anyone to do pretty much whatever they want with this code, as long as I get credit if a substantial part is copied* 

*to oversimplify a legal (and therefore very complicated) definition, substantial means a section of code pretty much directly copy-pasted beyond reasonable doubt (as far as this project is concerned)

[authme_mod]: https://github.com/axieum/authme
[dev_login_tool]: https://github.com/covers1624/DevLogin
[fabricmc_net]: https://fabricmc.net
[gapple_pw_mojang_api_docs]: https://mojang-api-docs.gapple.pw
[license]: LICENSE
[microsoft_entra_docs]: https://learn.microsoft.com/en-us/entra/identity-platform
[mod_menu_mod]: https://github.com/TerraformersMC/ModMenu
[multi_mc_launcher]: https://github.com/MultiMC/Launcher
[oauth_mod]: https://github.com/Sintinium/oauth
[reauth_mod]: https://github.com/TechnicianLP/ReAuth
[wiki_vg_microsoft_auth]: https://wiki.vg/Microsoft_Authentication_Scheme
<!---Yes, I used Auth Me's readme as my primary reference material when writing my readme... nothing was directly copy-pasted (beyond links) but the general outline & some syntax was re-used and adjusted for my own use--->
