import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:exercise_b/app.dart';

void main() {
  testWidgets('increments the counter', (WidgetTester tester) async {
    await tester.pumpWidget(const App());

    expect(find.text('0'), findsOneWidget);

    await tester.tap(find.byKey(const Key('increment')));
    await tester.pump();

    expect(find.text('1'), findsOneWidget);
  });

  testWidgets('decrements the counter', (WidgetTester tester) async {
    await tester.pumpWidget(const App());

    expect(find.text('0'), findsOneWidget);

    await tester.tap(find.byKey(const Key('decrement')));
    await tester.pump();

    expect(find.text('-1'), findsOneWidget);
  });
}
