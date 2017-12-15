namespace java com.github.thrift.service1.user
struct UserDTO{
	1: i64 id,
	2: string name
}
struct UserQuery{
	1: i64 id,
	2: string name
}
service UserService{
	UserDTO getById(1: i64 id),
	list<UserDTO> search(1: UserQuery query)
}