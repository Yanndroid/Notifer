import http.server


class RequestHandler(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        print(self.rfile.read(int(self.headers["Content-Length"])).decode("utf-8"))
        self.send_response(200)
        self.end_headers()


if __name__ == "__main__":
    http.server.HTTPServer(("", 8000), RequestHandler).serve_forever()
