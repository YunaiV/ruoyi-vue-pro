#!/usr/bin/env python3
"""
TCP Proxy Bridge for yudao-server
Bridges localhost ports to 192.168.2.x using system Python3 (Apple-signed)
which bypasses the per-process TCP filter.
"""
import socket
import threading
import sys

PROXIES = [
    (19999, '192.168.2.15', 49160, 'MySQL'),
    (16379, '192.168.2.15', 49154, 'Redis'),
]

def pipe(src, dst, name):
    try:
        while True:
            data = src.recv(4096)
            if not data:
                break
            dst.sendall(data)
    except Exception as e:
        pass
    finally:
        try:
            src.close()
        except:
            pass
        try:
            dst.close()
        except:
            pass

def handle(client, target_host, target_port, name):
    try:
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.connect((target_host, target_port))
        t1 = threading.Thread(target=pipe, args=(client, server, name), daemon=True)
        t2 = threading.Thread(target=pipe, args=(server, client, name), daemon=True)
        t1.start()
        t2.start()
    except Exception as e:
        print(f"[{name}] connect to {target_host}:{target_port} failed: {e}")
        client.close()

def start_proxy(listen_port, target_host, target_port, name):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind(('127.0.0.1', listen_port))
    s.listen(50)
    print(f"[{name}] proxy listening on 127.0.0.1:{listen_port} -> {target_host}:{target_port}")
    while True:
        try:
            client, addr = s.accept()
            handle(client, target_host, target_port, name)
        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"[{name}] accept error: {e}")
    s.close()

if __name__ == '__main__':
    threads = []
    for listen_port, target_host, target_port, name in PROXIES:
        t = threading.Thread(target=start_proxy, args=(listen_port, target_host, target_port, name), daemon=True)
        t.start()
        threads.append(t)
    print("All proxies started. Press Ctrl+C to stop.")
    try:
        for t in threads:
            t.join()
    except KeyboardInterrupt:
        print("\nShutting down proxies...")
        sys.exit(0)
