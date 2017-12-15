namespace java com.github.thrift.service1.order
struct OrderDTO{
	1: i64 id,
	2: string name
}
struct OrderQuery{
	1: i64 id,
	2: string name
}
service OrderService{
	OrderDTO getById(1: i64 id),
	list<OrderDTO> search(1: OrderQuery query)
}