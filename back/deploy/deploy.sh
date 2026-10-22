#!/bin/bash
# 사용법: deploy.sh <커밋 sha>
# S3에 올린 jar로 교체하고 재시작한다. 헬스 체크에 실패하면 직전 jar로 되돌린다.
set -euo pipefail

SHA="$1"
APP_DIR=/opt/mongkiz
RELEASE="$APP_DIR/releases/$SHA.jar"

aws s3 cp "s3://mongkiz-back-artifacts/releases/$SHA.jar" "$RELEASE" --region ap-northeast-2 --no-progress
chown mongkiz:mongkiz "$RELEASE"

PREV=$(readlink -e "$APP_DIR/app.jar" || true)
ln -sfn "$RELEASE" "$APP_DIR/app.jar"
systemctl restart mongkiz-back

for _ in $(seq 1 40); do
	if curl -fs http://127.0.0.1:8080/actuator/health | grep -q '"UP"'; then
		echo "deployed $SHA"
		# 최근 5개만 남긴다
		ls -1t "$APP_DIR"/releases/*.jar | tail -n +6 | xargs -r rm -f
		exit 0
	fi
	sleep 3
done

echo "health check failed: $SHA" >&2
journalctl -u mongkiz-back -n 80 --no-pager >&2 || true
if [ -n "$PREV" ] && [ "$PREV" != "$RELEASE" ]; then
	ln -sfn "$PREV" "$APP_DIR/app.jar"
	systemctl restart mongkiz-back
	echo "rolled back to $PREV" >&2
fi
exit 1
