package main

import (
	"log"
	"net"
	"net/http"
	"syscall"
)

func main() {
	// HTTPサーバを起動します。
	http.HandleFunc("/hogehoge", handleRequest)
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func handleRequest(w http.ResponseWriter, r *http.Request) {
	// TCP接続を取得します。
	hj, ok := w.(http.Hijacker)
	if !ok {
		http.Error(w, "webserver doesn't support hijacking", http.StatusInternalServerError)
		return
	}

	conn, _, err := hj.Hijack()
	if err != nil {
		http.Error(w, "failed to hijack connection", http.StatusInternalServerError)
		return
	}

	// TCP RSTパケットを送信して接続を閉じます。
	tcpConn, ok := conn.(*net.TCPConn)
	if !ok {
		log.Fatal("Not a TCP connection")
	}

	// ファイルディスクリプタを抽出します。
	fd, err := tcpConn.File()
	if err != nil {
		log.Fatal(err)
	}

	// ファイルディスクリプタを取得します。
	fileConn := (*fd).Fd()

	// RSTパケットを送信します。
	err = syscall.SetsockoptInt(int(fileConn), syscall.SOL_SOCKET, syscall.SO_LINGER, 0)
	if err != nil {
		log.Fatal(err)
	}

	conn.Close()
}

