[200~const http = require('http');
const net = require('net');

const server = http.createServer((req, res) => {
  if (req.url === '/hogehoge') {
    // リクエストが /hogehoge パスに対するものであれば接続をリセット
    let socket = req.socket;
    socket.destroy(new Error('RST'));
  } else {
    // それ以外のリクエストは通常通り処理
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Hello, World!\n');
  }
});

server.listen(8080, () => {
  console.log('Server running at http://127.0.0.1:8080/');
});

