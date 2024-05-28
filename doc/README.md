# CPD Assignment 2
## Matchmaking Server

### Compilation
Starting from the project root folder.

```bash
$ cd src
$ javac *.java
```

### Running the server
Starting from the `src` folder.

```bash
$ java GameServer <port> <credentials_file>
```

There is already a credentials file included in the project. You can run the following command:

```bash
$ java GameServer 8000 credentials.txt
```

### Running the client
Starting from the `src` folder.

```bash
$ java GameClient <server_ip> <port>
```

For example, you can run:
```bash
$ java GameClient 127.0.0.1 8000
```

You will then be prompted to authenticate. If you used the `credentials.txt` file already included, you can find the authentication credentials in the section bellow.

After authentication, you will be asked to choose between `simple` or `ranked` mode, and put in the respective queue.

Once matched, you can play `Tic-Tac-Toe` by inserting the row and column you want to play on (these are 0 indexed).
Before inputting the play, the player must press enter.

Finally, you can quit anytime by typing `quit`.

### Credentials

These are the credentials already included, that can be used for authentication.

| Username | Password      |
|----------|---------------|
| alice    | alicepassword  |
| bob    | bobpassword  |
| tiago    | tyga  |
| ruben    | ruben12345  |