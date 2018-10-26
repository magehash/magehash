-- name: reset-asset-contents
update asset
set content = null
where site = :site
returning *
