'use strict';
// Heuristic session quality scoring from observable signals (tool errors,
// retries). This is a coarse proxy, not a ground-truth judgment of whether a
// session "succeeded" -- there is no reliable exit-criteria signal in the
// transcript format, so error/retry rate is what we have. Documented as an
// approximation in tools/self-improve/README.md.
function scoreSession(session) {
  const denom = Math.max(session.toolUses, 1);
  const errorRate = session.toolErrors / denom;
  const retryRate = session.retries / denom;

  let score = 100 - errorRate * 100 * 0.6 - retryRate * 100 * 0.4;
  score = Math.max(0, Math.min(100, Math.round(score)));

  let label = 'medium';
  if (score >= 70) label = 'high';
  else if (score <= 40) label = 'low';

  return { score, label, errorRate, retryRate, toolUses: session.toolUses };
}

function scoreAll(sessions) {
  return sessions.map((s) => ({ session: s, ...scoreSession(s) }));
}

module.exports = { scoreSession, scoreAll };
