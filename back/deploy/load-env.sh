#!/bin/bash
# Parameter Store /mongkiz/prod/* 값을 systemd 환경 파일로 쓴다.
# - /run/mongkiz/back.env  : 백엔드 (mongkiz-back.service)
# - /run/mongkiz/caddy.env : ORIGIN_VERIFY만 (caddy.service)
set -euo pipefail

PREFIX=/mongkiz/prod/
OUT_DIR=/run/mongkiz

mkdir -p "$OUT_DIR"
umask 077

aws ssm get-parameters-by-path --path "$PREFIX" --with-decryption --region ap-northeast-2 \
	--query 'Parameters[].[Name,Value]' --output text \
	| awk -F'\t' -v prefix="$PREFIX" '{ sub("^" prefix, "", $1); print $1 "=" $2 }' > "$OUT_DIR/back.env.tmp"
mv "$OUT_DIR/back.env.tmp" "$OUT_DIR/back.env"

grep '^ORIGIN_VERIFY=' "$OUT_DIR/back.env" > "$OUT_DIR/caddy.env"
