# OpenDVS Architecture
![Overall architecture](opendvs_architecture.png)

## Core
Core of the platform, handles all user requests, secures the requests via `spring-security-oauth2` and RBAC. Forwards actions to other components via MQ. Also connects to `Fanout-type queue` for system-wide notification purposes (e.g. WebSocket) for proper clustering support.

## Resolver
Component that connects to same database source as *Core*. Handles resolved ProbeActions and PollerActions and resolves correct *Artifact* state. If dependency state cannot be determined, new *PollerAction* is triggered to find the correct implementation. 

## Probe
Component for decomposing applications to detect all components. Consists from two actions:
* Extraction
* Depdendency detection

Analysis is successfully completed after there are no *Extraction* and *Detection* steps available.
### Extraction
In order to detect packed dependencies (various archives, Docker containers, etc.) this step extracts resources into new folder which is later analysed by *Detection* step. This folder is only temporarily and is automatically deleted on the end of probing.

### Detection
Step that detects all dependencies as per defined implementation. Should provide at least following dependency information:
* Reconstructable unique identifier
* Name
* Group
* Version / Hash

## Poller
This component handles fetching of global (ATM) component metadata as per defined implementation, for example from Maven Central.
