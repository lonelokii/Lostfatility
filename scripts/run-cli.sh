#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

echo "=========================================="
echo " Launching The Lost Facility (Terminal CLI)"
echo "=========================================="
mvn exec:java -Dexec.mainClass="lostfacility.cli.CliApp" "$@"
