## Cached Auth
Cached Auth is an unfinished client-side Fabric mod I am working on that will use a device-code implementation of Minecraft's authentication api to allow users to set up accounts that are saved long term (similar to how MultiMC's account saving works).

Currently, it is being developed with the intention of personal use, and is unfinished.

### Note about Security
As of now, none of the obfuscation and encryption (for login information & mod api secrets) that I'd like to have is implemented, and as such, even once I have a "working" version, this mod will stay in a "please don't use" state
### Similar Mods
- [Auth Me][authme_mod] by axieum
- [ReAuth][reauth_mod] by TechnicianLP
- [OAuth][oauth_mod] by Sintinium

(These mods actually currently work, and were used as reference materials).


## To-Do list
Most of this stuff is roughly ordered by priority

### to get mod "working"
- [ ] finish oauth implementation and reauthenticating the client
  - [x] Microsoft Oauth2 device code flow request
  - [x] Microsoft Oauth2 device code flow polling
  - [ ] Xbox Live Authentication
  - [ ] XSTS Token for minecraft
  - [ ] Authenticate with minecraft
  - [ ] Check game ownership
  - [ ] fetch minecraft profile
- [ ] set up config saving/loading system to store client credentials (use JSON config)
  - [x] rough layout
  - [ ] integrate with fabric
- [ ] create and implement a gui into minecraft
  - [ ] Basic GUI and textures
  - [ ] Mod Menu integration

### Future plans
- clean up source code from development
  - keep my development comments alive in a development branch but filter it all out from master/release branch
  - revisit error handling and make it actually good (do I really need custom exception?)
  - remove unnecessary stuff from authentication json parsers & implement datetime handling where necessary
  - move some of authenticator stuff into AuthorizationSession and other relevant classes(?)
- Make the mod actually somewhat secure and protected
  - add section to README about how to raise security concerns privately
- Add debug mode
- publish to modrinth & maybe curseforge
- forge/neoforge versions? (need to bring myself up to date about what's going on there)
- allow for custom oauth urls & endpoints (similar to what Auth Me has)
- Offline only usernames support? (mostly useless except for allowing players to mess around with username-specific features like Notch's apple drop... also piracy) 

## License
Cached Auth is open-sourced software licensed under the [MIT license][license]

[authme_mod]: https://github.com/axieum/authme
[license]: LICENSE
[oauth_mod]: https://github.com/Sintinium/oauth
[reauth_mod]: https://github.com/TechnicianLP/ReAuth
<!---Yes, I used Auth Me's readme as my primary reference material when writing my readme... nothing was directly copy-pasted (beyond links) but the general outline & some syntax was re-used and adjusted for my own use--->