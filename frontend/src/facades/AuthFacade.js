import { AuthRepository } from '../repositories/AuthRepository.js'
const repository = new AuthRepository()
export const AuthFacade = { login(email,password){return repository.login(email,password)}, register(nombre,email,password){return repository.register(nombre,email,password)}, requestRecovery(email){return repository.requestRecovery(email)}, saveSession(data){const session={nombre:data.nombre,rol:data.rol,token:data.token};localStorage.setItem('usuario',JSON.stringify(session));return session}, clearSession(){localStorage.removeItem('usuario')} }
