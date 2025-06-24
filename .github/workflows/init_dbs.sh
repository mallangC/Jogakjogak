#!/bin/bash
mysql -u root -p rootpassword -e "CREATE DATABASE IF NOT EXISTS test_main_db;"
mysql -u root -p rootpassword -e "CREATE DATABASE IF NOT EXISTS test_meta_db;"