data_path="${1:-./csv_data}"
psql -f ./drop.sql
psql -f ./create.sql
psql -c "\copy person                       from $data_path/persons.csv WITH (FORMAT csv);"
psql -c "\copy contact_info                 from $data_path/contactInfos.csv WITH (FORMAT csv);"
psql -c "\copy driver                       from $data_path/drivers.csv WITH (FORMAT csv);"
psql -c "\copy customer                     from $data_path/customers.csv WITH (FORMAT csv);"
psql -c "\copy driver_status_history        from $data_path/driverStatusHistory.csv WITH (FORMAT csv);"
psql -c "\copy tariff_rate                  from $data_path/tariffRates.csv WITH (FORMAT csv);"
psql -c "\copy driver_license               from $data_path/driverLicenses.csv WITH (FORMAT csv);"
psql -c "\copy vehicle                      from $data_path/vehicles.csv WITH (FORMAT csv);"
psql -c "\copy vehicle_ownership            from $data_path/vehicleOwnerships.csv WITH (FORMAT csv);"
psql -c "\copy vehicle_movement_history     from $data_path/vehicleMovementHistory.csv WITH (FORMAT csv);"
psql -c "\copy orders                       from $data_path/orders.csv WITH (FORMAT csv);"
psql -c "\copy order_statuses               from $data_path/orderStatuses.csv WITH (FORMAT csv);"
psql -c "\copy cargo                        from $data_path/cargos.csv WITH (FORMAT csv);"
psql -c "\copy address                      from $data_path/addresses.csv WITH (FORMAT csv);"
psql -c "\copy storage_point                from $data_path/storagePoints.csv WITH (FORMAT csv);"
psql -c "\copy loading_unloading_agreement  from $data_path/loadingUnloadingAgreements.csv WITH (FORMAT csv);"
psql -c "\copy fuel_cards_for_drivers       from $data_path/fuelCardsForDrivers.csv WITH (FORMAT csv);"
psql -c "\copy fuel_expenses                from $data_path/fuelExpenses.csv WITH (FORMAT csv);"

psql -f ./set-sequences.sql
