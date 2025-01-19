# bdj-ipv6-hen
This is a BD-J Xlet that enables the execution of ELF payloads on PS5
consoles running firmwares 3.00-4.51. The Xlet uses a
[privilege escalation vulnerability][h1] discovered by [theflow][theflow],
which was later [reproduced for the PS4][insp1] by [sleirsgoevy][sleirsgoevy].
To escape the Java sandbox, the Xlet uses that vulnerability to disable the
security manager using a technique discovered by [sleirsgoevy][insp2].

The Xlet needs to be burned to a blueray disc, and includes a number
of useful payloads, e.g., an FTP server and a Telnet server. If your PS5 is
connected to the internet, these payloads are downloaded directly from
the web so you don't have to burn a new disc when they are updated.

## Quick-start
Download the [ISO][iso] and burn it to a bluray disc. Next, insert the disc into
your PS5 and navigate to Media to launch it.

## Building
See the [gihub CI action workflow][workflow].

## Reporting Bugs
If you encounter problems with bdj-ipv6-hen, please [file a github issue][issues].
If you plan on sending pull requests which affect more than a few lines of code,
please file an issue before you start to work on you changes. This will allow us
to discuss the solution properly before you commit time and effort.

## License
bdj-ipv6-hen is licensed under the GPLv3+.

[h1]: https://hackerone.com/reports/1379975
[insp1]: https://github.com/sleirsgoevy/bd-jb
[insp2]: https://github.com/sleirsgoevy/bd-jb/tree/ps5
[sleirsgoevy]: https://github.com/sleirsgoevy
[theflow]: https://github.com/TheOfficialFloW
[issues]: https://github.com/ps5-payload-dev/bdj-ipv6-hen/issues/new
[workflow]: https://github.com/ps5-payload-dev/bdj-ipv6-hen/blob/master/.github/workflows/ci.yml
[iso]: https://github.com/ps5-payload-dev/bdj-ipv6-hen/releases/latest/download/bdj-ipv6-hen.iso
