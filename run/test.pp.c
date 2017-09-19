int max(int a, int b) {
	if(a > b) {
		return a;
	} else {
		return b;
	}
}
int cal(int x, int y) {
	int i = 0;
	while(i < 10) {
		while(x < y) {
			x = x + i;
		}
		x = x * 2;
		y = x + y;
		i = i + 1;
	}

	return x;
}
int main() {
	int a, b;
	b = 2;
	a = b * 3 + b / 2 + 1;
	return 0;
}
