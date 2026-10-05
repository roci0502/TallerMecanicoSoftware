import { LocationRepository } from '../repositories/LocationRepository.js'
const repository=new LocationRepository()
export const LocationFacade={states(token){return repository.states(token)},municipalities(stateCode,token){return repository.municipalities(stateCode,token)},neighborhoods(stateCode,municipalityCode,token){return repository.neighborhoods(stateCode,municipalityCode,token)}}
