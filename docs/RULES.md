# Enrichers
## API:
EnricherExecutableParams
## Order
* global Enrichers
    * Labeling
    * Variables
    * Optional dependencies variables
* stack Enrichers
* environmentEnrichers
    * ON LOCAL:
        * LocalMountingEnricher
        * BuildingEnricher
        * SecretsEnricher
        * MandatoryEnvironmentsValidator
    * ON REMOTE
        * SwarmMountingPermissionsEnricher (zapewnia 1000 dla katalogów /shathel-data)
        * SwarmMountingEnricher (./ <- te rzeczy zamienia na /shathel-data i kopiuje pliki)
        * SwarmPullingEnricher
        * SecretsEnricher
        * MandatoryEnvironmentsValidator
        * SwarmStickyVolumeEnricher


# PROVISIONERS:
## API
ProvisionerExecutableParams