
/**
 * Socket消息监听器.
 * 
 * @zhuowei.luo
 */
function SocketEventBus() {
	
	// 消息类型对应的监听方法对象
	this.listenerMap = {};
	
	/**
	 * 注册监听
	 * 
	 * @param socketName 监听的消息类型名称
	 * @param fun 处理该消息的具体方法
	 */
	this.register = function(socketName, fun) {
		if(socketName != null && socketName != "" && typeof fun === "function") {
			if(this.listenerMap[socketName] == null) {
				this.listenerMap[socketName] = [];
			}
			this.listenerMap[socketName].push(fun);
		}
	}
	
	/**
	 * 发布消息
	 * 
	 * @param socketMsg 消息主体
	 */
	this.post = function(socketMsg) {
		var socketName;
		if(socketMsg != null && (socketName = socketMsg["socketName"]) != null) {
			var funs = this.listenerMap[socketName];
			if(funs != null && funs.length > 0) {
				for(var index in funs) {
					funs[index](socketMsg);
				}
			}
		}
	}
}
