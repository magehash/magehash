.PHONY: test

test:
	COAST_ENV=test clj -A\:test

clean:
	rm -rf target

uberjar:
	clj -A\:uberjar

server:
	clj -m server

repl:
	clj -R:repl bin/repl.clj

db/migrate:
	clj -A\:db/migrate

db/rollback:
	clj -A\:db/rollback

db/create:
	clj -A\:db/create

db/drop:
	clj -A\:db/drop

worker:
	clj -m coast.worker

assets:
	clj -m coast.assets

routes:
	clj -m coast.router
