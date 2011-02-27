# dredd

Simple Online Automated Judge System in Clojure. Mainly used in my Programming course so many things in it are ad-hoc. Moreover, many texts are not in english language, as it is not used in english speaking country.

## Installation and Usage

* Copy dredd.local_settings_example.clj into dredd.local_settings.clj and customize it

* Starting dredd:
    * screen ./start.sh
    * then press C-a d

* Shutdown:
    * Use administrator web interface

* Force shutdown (database may get corrupted):
    * resume with screen -r
    * then C-c to shutdown server

* Hint: Start dredd on a dedicated port and use reverse proxy to make it public. That way you can also enable TLS for dredd.

## Documentation

As this is a literate programming attempt, documentation is in sources

Start reading at dredd/server.clj

## License

Copyright (C) 2011, Jozef Wagner. All rights reserved.

Contact: <jozef.wagner@gmail.com> or <wagjo@wagjo.com>
