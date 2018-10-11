# magehash

## Start

```bash
git clone git@github.com:your-user-name/magehash.git
cd magehash
cp .env.fake .env # replace any values
make db/create
make db/migrate
```

## Dev

```bash
make repl # starts an nrepl server
```

```clojure
; in your editor, connect to the nrepl server
; in atom with proto-repl, it's Ctrl+Cmd+Y then enter
; then when you're in the repl type evaluate -main
(-main)
```

```bash
curl http://localhost:1337 # or just open it in your browser
```

## Ship
```bash
make db/migrate
make assets
make uberjar
java -jar target/magehash-1.0.0-standalone.jar -m server 1337
```
