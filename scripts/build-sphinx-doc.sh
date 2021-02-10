#!/usr/bin/env bash
#
# Builds ginipaybank's sphinx documentation.
#
# Must be executed from the project root.
#
set -e
#set -x

cd ginipaybank/src/doc
virtualenv ./virtualenv
source virtualenv/bin/activate
pip install -r requirements.txt

make clean
make html singlehtml